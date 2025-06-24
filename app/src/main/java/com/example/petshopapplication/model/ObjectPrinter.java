package com.example.petshopapplication.model;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
public class ObjectPrinter {
    private static final int MAX_DEPTH = 10;
    private static final String INDENT = "    ";

    public static String print(Object obj) {
        if (obj == null) return "null";
        return print(obj, 0, new HashSet<>());
    }

    private static String print(Object obj, int depth, Set<Object> visited) {
        if (obj == null) return "null";
        if (depth > MAX_DEPTH) return "...";
        if (!shouldRecurse(obj)) return obj.toString();
        if (visited.contains(obj)) return "[CIRCULAR REFERENCE]";

        visited.add(obj);
        StringBuilder sb = new StringBuilder();
        String indent = INDENT.repeat(depth);

        // Handle different types of objects
        if (obj instanceof Collection<?>) {
            printCollection((Collection<?>) obj, sb, depth, visited);
        } else if (obj instanceof Map<?, ?>) {
            printMap((Map<?, ?>) obj, sb, depth, visited);
        } else {
            printObject(obj, sb, depth, visited);
        }

        visited.remove(obj);
        return sb.toString();
    }

    private static void printCollection(Collection<?> collection, StringBuilder sb, int depth, Set<Object> visited) {
        String indent = INDENT.repeat(depth);
        sb.append(collection.getClass().getSimpleName()).append(" [\n");
        for (Object item : collection) {
            sb.append(indent).append(INDENT)
                    .append(print(item, depth + 1, visited))
                    .append("\n");
        }
        sb.append(indent).append("]");
    }

    private static void printMap(Map<?, ?> map, StringBuilder sb, int depth, Set<Object> visited) {
        String indent = INDENT.repeat(depth);
        sb.append(map.getClass().getSimpleName()).append(" {\n");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append(indent).append(INDENT)
                    .append(print(entry.getKey(), depth + 1, visited))
                    .append(" -> ")
                    .append(print(entry.getValue(), depth + 1, visited))
                    .append("\n");
        }
        sb.append(indent).append("}");
    }

    private static void printObject(Object obj, StringBuilder sb, int depth, Set<Object> visited) {
        String indent = INDENT.repeat(depth);
        sb.append(obj.getClass().getSimpleName()).append(" {\n");

        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                sb.append(indent).append(INDENT)
                        .append(field.getName())
                        .append(": ")
                        .append(print(value, depth + 1, visited))
                        .append("\n");
            } catch (IllegalAccessException e) {
                sb.append(indent).append(INDENT)
                        .append(field.getName())
                        .append(": [ACCESS DENIED]\n");
            }
        }
        sb.append(indent).append("}");
    }

    private static boolean shouldRecurse(Object obj) {
        return obj instanceof Collection<?> ||
                obj instanceof Map<?, ?> ||
                (obj != null && obj.getClass().getPackage() != null &&
                        !obj.getClass().getPackage().getName().startsWith("java"));
    }


}