package com.raishxn.ufo.client;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the compiled selectors against the actual AE2 dependency without loading client classes. */
class CpuSelectionMixinCompatibilityTest {
    private static final String INJECTION = "Lorg/spongepowered/asm/mixin/injection/";

    @Test
    void requiredHooksAndVersionGroupsMatchAe2Bytecode() throws IOException {
        var target = readClass("appeng/client/gui/widgets/CPUSelectionList");
        var mixin = readClass("com/raishxn/ufo/mixin/MixinCPUSelectionList");
        Map<String, Integer> groupMatches = new HashMap<>();
        Map<String, AnnotationNode> groups = new HashMap<>();
        int checkedHooks = 0;
        for (var handler : mixin.methods) {
            var group = annotation(handler, INJECTION + "Group;");
            for (String type : List.of("Inject", "Redirect", "ModifyArg")) {
                var injection = annotation(handler, INJECTION + type + ";");
                if (injection == null) continue;
                checkedHooks++;
                Object selectors = value(injection, "method");
                assertTrue(selectors instanceof List<?>, handler.name + " has no method selector");
                var atValue = value(injection, "at");
                var at = atValue instanceof List<?> list ? (AnnotationNode) list.getFirst()
                        : (AnnotationNode) atValue;
                int matches = 0;
                for (var method : target.methods) {
                    if (!((List<?>) selectors).contains(method.name)) continue;
                    if ("HEAD".equals(value(at, "value"))) {
                        matches++;
                    } else {
                        assertEquals("INVOKE", value(at, "value"), "Unsupported selector in " + handler.name);
                        for (var instruction : method.instructions) {
                            if (instruction instanceof MethodInsnNode call
                                    && ("L" + call.owner + ";" + call.name + call.desc)
                                            .equals(value(at, "target"))) {
                                matches++;
                            }
                        }
                    }
                }
                Object require = value(injection, "require");
                int minimum = require instanceof Integer count ? count : 1;
                assertTrue(matches >= minimum, handler.name + " is missing a required AE2 target");
                if (group != null) {
                    String name = (String) value(group, "name");
                    groups.put(name, group);
                    groupMatches.merge(name, matches, Integer::sum);
                }
            }
        }
        assertTrue(checkedHooks > 0, "No compiled injection annotations were inspected");
        assertEquals(2, groups.size(), "Both version-dependent formatters must be checked");
        for (var entry : groups.entrySet()) {
            int matches = groupMatches.get(entry.getKey());
            assertTrue(matches >= (Integer) value(entry.getValue(), "min"), entry.getKey());
            assertTrue(matches <= (Integer) value(entry.getValue(), "max"), entry.getKey());
        }
    }

    private static ClassNode readClass(String name) throws IOException {
        try (var stream = CpuSelectionMixinCompatibilityTest.class.getResourceAsStream("/" + name + ".class")) {
            assertNotNull(stream, name + " must be on the test classpath");
            var node = new ClassNode();
            new ClassReader(stream).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return node;
        }
    }

    private static AnnotationNode annotation(MethodNode method, String descriptor) {
        for (var annotations : List.of(method.visibleAnnotations == null ? List.<AnnotationNode>of()
                : method.visibleAnnotations, method.invisibleAnnotations == null ? List.<AnnotationNode>of()
                : method.invisibleAnnotations)) {
            for (var annotation : annotations) {
                if (descriptor.equals(annotation.desc)) return annotation;
            }
        }
        return null;
    }

    private static Object value(AnnotationNode annotation, String name) {
        if (annotation.values != null) {
            for (int i = 0; i < annotation.values.size(); i += 2) {
                if (name.equals(annotation.values.get(i))) return annotation.values.get(i + 1);
            }
        }
        return null;
    }
}
