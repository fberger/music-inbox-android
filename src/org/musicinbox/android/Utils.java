package org.musicinbox.android;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class Utils {

	public static void close(HttpResponse response) {
		if (response != null) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (IOException e) {
				}
			}
		}
	}
	

    private static ThreadLocal<IdentityHashMap<Object, Object>> threadLocal = new ThreadLocal<IdentityHashMap<Object, Object>>();

    /**
     * Creates a string representation of the object <code>thiz</code>.
     * <p>
     * Can optionally be given a whitelist of fields that should be part of the
     * string output.
     * <p>
     * Note: Should synchronize calling method if the fields of the instance can
     * be modified by other threads.
     * <p>
     * Note: Creates a temporary copy of arrays of primitive elements.
     * <p>
     * Calls {@link Object#toString()} on fields.
     */
    public static String toString(Object thiz, Object... whitelist) {
        return toStringBlackAndWhite(thiz, Arrays.asList(whitelist), Collections.emptyList());
    }

    /**
     * Creates a string representation of the object <code>thiz</code>.
     * <p>
     * Can optionally be given a blacklist of fields that should not be part of
     * the string output.
     * <p>
     * Note: Should synchronize calling method if the fields of the instance can
     * be modified by other threads.
     * <p>
     * Note: Creates a temporary copy of arrays of primitive elements.
     * <p>
     * Calls {@link Object#toString()} on fields.
     */
    public static String toStringBlacklist(Object thiz, Object... blacklist) {
        return toStringBlackAndWhite(thiz, Collections.emptyList(), Arrays.asList(blacklist));
    }

    /**
     * Creates a string representation of the object <code>thiz</code>.
     * <p>
     * Can optionally be given a blacklist and whitelist of fields that should
     * not be part of the string output.
     * <p>
     * Note: Should synchronize calling method if the fields of the instance can
     * be modified by other threads.
     * <p>
     * Note: Creates a temporary copy of arrays of primitive elements.
     * <p>
     * Calls {@link Object#toString()} on fields.
     */
    private static String toStringBlackAndWhite(Object thiz,
            Collection<? extends Object> whitelist, Collection<? extends Object> blacklist) {
        boolean cleanUp = false;
        try {
            IdentityHashMap<Object, Object> handledObjects = threadLocal.get();
            if (handledObjects == null) {
                cleanUp = true;
                handledObjects = new IdentityHashMap<Object, Object>();
                threadLocal.set(handledObjects);
            }
            if (handledObjects.containsKey(thiz)) {
                return "circular structure";
            }
            handledObjects.put(thiz, thiz);
            Map<String, String> fields = new LinkedHashMap<String, String>();
            for (Field field : thiz.getClass().getDeclaredFields()) {
                try {
                    boolean accessible = field.isAccessible();
                    field.setAccessible(true);
                    Object value = field.get(thiz);
                    field.setAccessible(accessible);
                    if (!Modifier.isStatic(field.getModifiers()) && !blacklist.contains(value)
                            && (whitelist.isEmpty() || whitelist.contains(value))) {
                        if (value == null) {
                            fields.put(field.getName(), String.valueOf(value));
                        } else {
                            Class<?> clazz = value.getClass();
                            if (clazz.isArray()) {
                                if (!clazz.getComponentType().isPrimitive()) {
                                    fields.put(field.getName(), String.valueOf(Arrays
                                            .asList((Object[]) value)));
                                } else {
                                    int length = Array.getLength(value);
                                    List<Object> copy = new ArrayList<Object>(length);
                                    for (int i = 0; i < length; i++) {
                                        copy.add(Array.get(value, i));
                                    }
                                    fields.put(field.getName(), String.valueOf(copy));
                                }
                            } else {
                                fields.put(field.getName(), String.valueOf(value));
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return thiz.getClass().getSimpleName() + " " + fields.toString();
        } finally {
            if (cleanUp) {
                threadLocal.set(null);
            }
        }
    }

	
}
