package ru.alexander.z3;

import com.google.gson.*;

import java.util.*;

public class SToJSON {
    private final Gson gson;
    public SToJSON(boolean prettyPrinting) {
        GsonBuilder builder = new GsonBuilder();
        if (prettyPrinting) builder.setPrettyPrinting();
        gson = builder.create();
    }

    public String transform(String sExpr) {
        return gson.toJson(parse(sExpr));
    }

    private JsonObject parse(String sExpr) {
        Object[] data = (Object[]) parseSExpression(sExpr)[0];
        Object map = buildTree(data);
        return (JsonObject) encodeJsonHierarchy(map);
    }

//    Грамматика S-выражений в БНФ:
//            <s-exp> ::= <atom> | '(' <s-exp-list> ')'
//            <s-exp-list> ::= <sexp> <s-exp-list> |
//            <atom> ::= <symbol> | <integer> | #t | #f
//    Пример S-выражения:
//            (users
//            ((uid 1) (name root) (gid 1))
//            ((uid 108) (name peter) (gid 108))
//            ((uid 109) (name alex) (gid 109)))

    private Object[] parseSExpression(String sExpr) {
        sExpr = sExpr.replace("\r", " ").replace("\n", " ");
        List<Object> list = new ArrayList<>();
        int level = 0;
        int start = 0;
        boolean first = true;

        for (int i = 0; i < sExpr.length(); i++) {
            if (sExpr.charAt(i) == '(') {
                if (level == 0) start = i;
                level++;
            }
            else if (sExpr.charAt(i) == ')') {
                level--;
                if (level == 0) {
                    if (first && start != 0)
                        list.add(sExpr.substring(0, start).replace(" ", ""));

                    list.add(parseSExpression(sExpr.substring(start + 1, i)));
                    first = false;
                }
            }
        }
        if (first) {
            int pos = sExpr.indexOf(" ");
            list.add(sExpr.substring(0, pos));
            list.add(sExpr.substring(pos + 1));
        }
        return list.toArray(Object[]::new);
    }

    private Object buildTree(Object[] data) {
        Object object;
        if (data[0].getClass() == String.class) {
            boolean mapFlag = true;
            for (int i = 1; i < data.length; i++) {
                if (data[i].getClass() == Object[].class) {
                    Object[] val = (Object[]) data[i];
                    if (val.length != 2 || val[0].getClass() != String.class) {
                        mapFlag = false;
                        break;
                    }
                }
                else {
                    mapFlag = false;
                    break;
                }
            }
            if (mapFlag) {
                HashMap<String, Object> map = new HashMap<>();
                for (int i = 1; i < data.length; i++) {
                    Object[] val = (Object[]) data[i];
                    if (val[1].getClass() == Object[].class)
                        map.put((String) val[0], buildTree((Object[]) val[1]));
                    else map.put((String) val[0], val[1]);
                }
                object = map;
            }
            else {
                Object[] arr = new Object[data.length - 1];
                for (int i = 1; i < data.length; i++)
                    arr[i - 1] = buildTree((Object[]) data[i]);
                object = arr;
            }
        }
        else {
            HashMap<String, Object> map = new HashMap<>();
            for (int i = 0; i < data.length; i++) {
                Object[] val = (Object[]) data[i];
                if (val[1].getClass() == Object[].class)
                    map.put((String) val[0], buildTree(val));
                else map.put((String) val[0], val[1]);
            }
            object = map;
        }
        return object;
    }
    private JsonElement encodeJsonHierarchy(Object object) {
        JsonElement element;
        Class<?> cl = object.getClass();
        if (cl.isArray()) {
            List<Object> list = Arrays.asList((Object[]) object);
            JsonArray array = new JsonArray(list.size());
            for (Object o : list)
                array.add(encodeJsonHierarchy(o));
            element = array;
        } else if (Arrays.stream(cl.getInterfaces()).anyMatch(i -> i == Map.class)) {
            JsonObject obj = new JsonObject();
            Map<String, Object> map = (Map<String, Object>) object;
            map.forEach((name, prop) -> obj.add(name, encodeJsonHierarchy(prop)));
            element = obj;
        }
        else if (object instanceof String str) {
            try {
                element = new JsonPrimitive(Long.parseLong(str));
            } catch (Exception e) {
                try {
                    element = new JsonPrimitive(Double.parseDouble(str));
                } catch (Exception e1) {
                    element = new JsonPrimitive(str);
                }
            }
        }
        else throw new IllegalStateException("Wrong tree data format!");

        return element;
    }
}
