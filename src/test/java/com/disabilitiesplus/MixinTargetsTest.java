package com.disabilitiesplus;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

class MixinTargetsTest {
    private static final String PKG_DIR = "com/disabilitiesplus/mixin/";
    private static final Map<String, ClassNode> CACHE = new HashMap<>();
    private static int checks = 0;

    private static ClassNode load(String internalName) {
        return CACHE.computeIfAbsent(internalName, n -> {
            try (InputStream in = ClassLoader.getSystemResourceAsStream(n + ".class")) {
                if (in == null) {
                    return null;
                }

                ClassNode cn = new ClassNode();
                new ClassReader(in).accept(cn, ClassReader.SKIP_FRAMES);
                return cn;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static List<String> configNames() throws IOException {
        String json;
        try (InputStream in = MixinTargetsTest.class.getResourceAsStream("/disabilities_plus.mixins.json")) {
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<String> out = new ArrayList<>();
        for (String key : new String[]{"mixins", "client", "server"}) {
            Matcher arr = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL).matcher(json);
            if (arr.find()) {
                Matcher s = Pattern.compile("\"([^\"]+)\"").matcher(arr.group(1));
                while (s.find()) {
                    out.add(s.group(1));
                }
            }
        }

        return out;
    }

    @Test
    void everyMixinClassOnDiskIsRegisteredAndEveryRegisteredOneExists() throws Exception {
        Set<String> registered = new HashSet<>(configNames());
        List<String> problems = new ArrayList<>();

        for (String name : registered) {
            if (load(PKG_DIR + name) == null) {
                problems.add("registered but missing class: " + name);
            }
        }

        Path dir = Path.of("build/classes/java/main/" + PKG_DIR);
        if (Files.isDirectory(dir)) {
            try (Stream<Path> files = Files.list(dir)) {
                files.map(p -> p.getFileName().toString())
                    .filter(f -> f.endsWith(".class") && !f.contains("$"))
                    .map(f -> f.substring(0, f.length() - 6))
                    .filter(n -> !registered.contains(n))
                    .forEach(n -> problems.add("class exists but is NOT in disabilities.mixins.json: " + n));
            }
        }

        assertTrue(problems.isEmpty(), String.join("\n", problems));
        assertTrue(registered.size() >= 19, "expected at least the add-on's 19 mixins, got " + registered.size());
    }

    @Test
    void everyMixinTargetResolvesAgainstMinecraft() throws Exception {
        List<String> problems = new ArrayList<>();
        int mixinsChecked = 0;

        for (String name : configNames()) {
            ClassNode mixin = load(PKG_DIR + name);
            if (mixin == null) {
                continue;
            }

            mixinsChecked++;
            List<ClassNode> targets = resolveTargets(mixin, problems);
            for (MethodNode m : mixin.methods) {
                for (AnnotationNode a : all(m.invisibleAnnotations, m.visibleAnnotations)) {
                    checkMethodAnnotation(name, m, a, targets, problems);
                }
            }
        }

        System.out.println("[MixinTargetsTest] mixins=" + mixinsChecked + " assertions=" + checks);
        assertTrue(mixinsChecked >= 19);
        assertTrue(checks > 20, "suspiciously few checks ran: " + checks);
        assertTrue(problems.isEmpty(), "\n" + String.join("\n", problems));
    }

    private static List<ClassNode> resolveTargets(ClassNode mixin, List<String> problems) {
        List<ClassNode> out = new ArrayList<>();
        for (AnnotationNode a : all(mixin.invisibleAnnotations, mixin.visibleAnnotations)) {
            if (!a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
                continue;
            }

            for (Object v : list(get(a, "value"))) {
                String internal = ((Type)v).getInternalName();
                ClassNode t = load(internal);
                checks++;
                if (t == null) {
                    problems.add(mixin.name + ": target class not found: " + internal);
                } else {
                    out.add(t);
                }
            }

            for (Object v : list(get(a, "targets"))) {
                String internal = ((String)v).replace('.', '/');
                ClassNode t = load(internal);
                checks++;
                if (t == null) {
                    problems.add(mixin.name + ": target class not found: " + internal);
                } else {
                    out.add(t);
                }
            }
        }

        return out;
    }

    private static void checkMethodAnnotation(String mixinName, MethodNode handler, AnnotationNode a, List<ClassNode> targets, List<String> problems) {
        String d = a.desc;
        String where = mixinName + "." + handler.name;
        boolean injector = d.equals("Lorg/spongepowered/asm/mixin/injection/Inject;")
            || d.equals("Lorg/spongepowered/asm/mixin/injection/Redirect;")
            || d.equals("Lorg/spongepowered/asm/mixin/injection/ModifyVariable;")
            || d.equals("Lorg/spongepowered/asm/mixin/injection/ModifyArg;")
            || d.equals("Lorg/spongepowered/asm/mixin/injection/ModifyConstant;");

        if (injector) {
            List<String> selectors = new ArrayList<>();
            for (Object o : list(get(a, "method"))) {
                selectors.add((String)o);
            }

            if (selectors.isEmpty()) {
                problems.add(where + ": injector has no method selector");
                return;
            }

            for (ClassNode target : targets) {
                List<MethodNode> resolved = new ArrayList<>();
                for (String sel : selectors) {
                    List<MethodNode> found = find(target, sel);
                    checks++;
                    if (found.isEmpty()) {
                        problems.add(where + ": no method '" + sel + "' declared in " + target.name);
                    }

                    resolved.addAll(found);
                }

                if (d.endsWith("Inject;")) {
                    for (MethodNode t : resolved) {
                        checkInjectSignature(where, handler, t, problems);
                    }
                }

                if (d.endsWith("ModifyVariable;") && Boolean.TRUE.equals(get(a, "argsOnly"))) {
                    Type want = Type.getReturnType(handler.desc);
                    for (MethodNode t : resolved) {
                        checks++;
                        long n = java.util.Arrays.stream(Type.getArgumentTypes(t.desc)).filter(x -> x.equals(want)).count();
                        if (n != 1 && get(a, "ordinal") == null) {
                            problems.add(where + ": argsOnly ModifyVariable of " + want + " is ambiguous/missing in " + t.name + t.desc + " (" + n + " candidates)");
                        }
                    }
                }

                Object at = get(a, "at");
                List<AnnotationNode> ats = new ArrayList<>();
                if (at instanceof AnnotationNode an) {
                    ats.add(an);
                } else {
                    for (Object o : list(at)) {
                        ats.add((AnnotationNode)o);
                    }
                }

                for (AnnotationNode atNode : ats) {
                    checkAt(where, atNode, resolved, problems);
                }
            }
        } else if (d.equals("Lorg/spongepowered/asm/mixin/gen/Accessor;")) {
            String field = (String)get(a, "value");
            for (ClassNode target : targets) {
                checks++;
                FieldNode f = target.fields.stream().filter(x -> x.name.equals(field)).findFirst().orElse(null);
                if (f == null) {
                    problems.add(where + ": @Accessor field '" + field + "' not found in " + target.name);
                } else {
                    Type[] args = Type.getArgumentTypes(handler.desc);
                    Type shape = args.length == 1 ? args[0] : Type.getReturnType(handler.desc);
                    if (!Type.getType(f.desc).equals(shape)) {
                        problems.add(where + ": @Accessor type " + shape + " != field type " + f.desc);
                    }
                }
            }
        } else if (d.equals("Lorg/spongepowered/asm/mixin/gen/Invoker;")) {
            String method = (String)get(a, "value");
            for (ClassNode target : targets) {
                checks++;
                MethodNode m = target.methods.stream()
                    .filter(x -> x.name.equals(method) && java.util.Arrays.equals(Type.getArgumentTypes(x.desc), Type.getArgumentTypes(handler.desc)))
                    .findFirst()
                    .orElse(null);
                if (m == null) {
                    problems.add(where + ": @Invoker '" + method + Type.getMethodType(handler.desc).getDescriptor() + "' not found in " + target.name);
                } else if (!Type.getReturnType(m.desc).equals(Type.getReturnType(handler.desc))) {
                    problems.add(where + ": @Invoker return type differs from " + m.desc);
                }
            }
        }
    }

    private static void checkInjectSignature(String where, MethodNode handler, MethodNode target, List<String> problems) {
        checks++;
        Type[] have = Type.getArgumentTypes(handler.desc);
        Type[] targetArgs = Type.getArgumentTypes(target.desc);
        boolean returnsValue = Type.getReturnType(target.desc).getSort() != Type.VOID;
        String ci = returnsValue
            ? "org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable"
            : "org/spongepowered/asm/mixin/injection/callback/CallbackInfo";

        if (have.length == 0 || !have[have.length - 1].getInternalName().equals(ci)) {
            problems.add(where + ": last parameter must be " + ci + " for target " + target.name + target.desc);
            return;
        }

        boolean bare = have.length == 1;
        boolean full = have.length == targetArgs.length + 1;
        if (full) {
            for (int i = 0; i < targetArgs.length; i++) {
                if (!have[i].equals(targetArgs[i])) {
                    problems.add(where + ": parameter " + i + " is " + have[i] + " but target " + target.name + " takes " + targetArgs[i]);
                }
            }
        } else if (!bare) {
            problems.add(where + ": handler takes " + (have.length - 1) + " args, target " + target.name + target.desc + " takes " + targetArgs.length);
        }
    }

    private static void checkAt(String where, AnnotationNode at, List<MethodNode> resolved, List<String> problems) {
        String kind = (String)get(at, "value");
        String target = (String)get(at, "target");
        if (kind == null || target == null) {
            return;
        }

        if (kind.startsWith("INVOKE")) {
            int semi = target.indexOf(';');
            int paren = target.indexOf('(');
            if (!target.startsWith("L") || semi < 0 || paren < 0) {
                problems.add(where + ": unparseable INVOKE target " + target);
                return;
            }

            String owner = target.substring(1, semi);
            String name = target.substring(semi + 1, paren);
            String desc = target.substring(paren);
            checks++;
            boolean present = resolved.stream().anyMatch(m -> contains(m, owner, name, desc));
            if (!present) {
                problems.add(where + ": INVOKE " + owner + "." + name + desc + " never appears in " + names(resolved));
            }
        } else if (kind.equals("FIELD")) {
            int semi = target.indexOf(';');
            int colon = target.indexOf(':');
            String owner = target.substring(1, semi);
            String name = target.substring(semi + 1, colon);
            checks++;
            boolean present = resolved.stream().anyMatch(m -> {
                for (AbstractInsnNode i : m.instructions) {
                    if (i instanceof FieldInsnNode f && f.owner.equals(owner) && f.name.equals(name)) {
                        return true;
                    }
                }

                return false;
            });
            if (!present) {
                problems.add(where + ": FIELD " + owner + "." + name + " never appears in " + names(resolved));
            }
        }
    }

    private static boolean contains(MethodNode m, String owner, String name, String desc) {
        for (AbstractInsnNode i : m.instructions) {
            if (i instanceof MethodInsnNode mi && mi.owner.equals(owner) && mi.name.equals(name) && mi.desc.equals(desc)) {
                return true;
            }
        }

        return false;
    }

    private static String names(List<MethodNode> ms) {
        List<String> n = new ArrayList<>();
        for (MethodNode m : ms) {
            n.add(m.name + m.desc);
        }

        return n.toString();
    }

    private static List<MethodNode> find(ClassNode target, String selector) {
        String s = selector;
        if (s.startsWith("L") && s.indexOf(';') > 0 && s.indexOf(';') < (s.contains("(") ? s.indexOf('(') : s.length())) {
            s = s.substring(s.indexOf(';') + 1);
        }

        int paren = s.indexOf('(');
        String name = paren < 0 ? s : s.substring(0, paren);
        String desc = paren < 0 ? null : s.substring(paren);
        List<MethodNode> out = new ArrayList<>();
        for (MethodNode m : target.methods) {
            boolean bridge = (m.access & 0x40) != 0;
            if (!bridge && m.name.equals(name) && (desc == null || m.desc.equals(desc))) {
                out.add(m);
            }
        }

        return out;
    }

    @SafeVarargs
    private static <T> List<T> all(List<T>... lists) {
        List<T> out = new ArrayList<>();
        for (List<T> l : lists) {
            if (l != null) {
                out.addAll(l);
            }
        }

        return out;
    }

    private static Object get(AnnotationNode a, String key) {
        if (a.values == null) {
            return null;
        }

        for (int i = 0; i + 1 < a.values.size(); i += 2) {
            if (a.values.get(i).equals(key)) {
                return a.values.get(i + 1);
            }
        }

        return null;
    }

    private static List<?> list(Object o) {
        if (o == null) {
            return List.of();
        }

        return o instanceof List<?> l ? l : List.of(o);
    }
}
