package xoled.java.misc.subject;

import com.google.common.base.Defaults;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import org.junit.Test;

import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class SubjectTest {

    @Test
    public void rendering() {
        final Subject<Bean> subject = new Subject<>(Bean.class)
            .add("beans")
            .add(Bean::int32)
            .add(Bean::int64)
            .add(Bean::str)
            .add(Bean::real)
            .add(Bean::decimal)
            ;
        final Bean bean = new Bean() {
            @Override
            public int int32() {
                return 1234;
            }

            @Override
            public long int64() {
                return 5678;
            }

            @Override
            public double real() {
                return 3.14159;
            }

            @Override
            public boolean bool() {
                return true;
            }

            @Override
            public String str() {
                return "Alpha";
            }

            @Override
            public BigDecimal decimal() {
                return new BigDecimal("1.23456789");
            }
        };
        final String path = subject.render(bean, "/");
        assertThat(path).isEqualTo("/beans/1234/5678/Alpha/3.14159/1.23456789");
    }

    @Test
    public void accept_valid_path() {
        final Subject<Bean> subject = new Subject<>(Bean.class)
            .add("beans")
            .add(Bean::int32)
            .add(Bean::int64)
            .add(Bean::str);
        final boolean matched = subject.match(
            Iterators.forArray("beans", "1234", "5678", "Alpha")
        );
        assertThat(matched).isTrue();
    }

    @Test
    public void reject_shorter_path() {
        final Subject<Bean> subject = new Subject<>(Bean.class)
            .add("beans")
            .add(Bean::int32)
            .add(Bean::int64)
            .add(Bean::str);
        final boolean matched = subject.match(
            Iterators.forArray("beans", "1234", "5678")
        );
        assertThat(matched).isFalse();
    }

    @Test
    public void reject_longer_path() {
        final Subject<Bean> subject = new Subject<>(Bean.class)
            .add("beans")
            .add(Bean::int32)
            .add(Bean::int64)
            .add(Bean::str);
        final boolean matched = subject.match(
            Iterators.forArray("beans", "1234", "5678", "ABC", "098")
        );
        assertThat(matched).isFalse();
    }

    @Test
    public void reject_if_pattern_does_not_match() {
        final Subject<Bean> subject = new Subject<>(Bean.class)
            .add("beans")
            .add(Bean::int32)
            .add(Bean::real)
            .add(Bean::str);
        final boolean matched = subject.match(
            Iterators.forArray("beans", "1234", "NOT A REAL", "ABC")
        );
        assertThat(matched).isFalse();
    }

    @Test
    public void accept_with_boolean() {
        final Subject<Bean> subject = new Subject<>(Bean.class)
            .add("beans")
            .add(Bean::int32)
            .add(Bean::bool);
        final boolean matched = subject.match(
            Iterators.forArray("beans", "1234", "true")
        );
        assertThat(matched).isTrue();
    }

    @Test
    public void accept_with_decimal() {
        final Subject<Bean> subject = new Subject<>(Bean.class)
            .add("beans")
            .add(Bean::str)
            .add(Bean::decimal);
        final boolean matched = subject.match(
            Iterators.forArray("beans", "something", "-100.12345678")
        );
        assertThat(matched).isTrue();
    }

    @Test
    public void accept_after_split() {
        final Subject<Bean> subject = new Subject<>(Bean.class)
            .add("beans")
            .add(Bean::int32)
            .add(Bean::int64)
            .add(Bean::str)
            .add(Bean::real);

        final Iterator<? extends CharSequence> elements =
            new Scanner(new StringReader("/beans/1234/5678/Alpha/3.14159")).useDelimiter("/");
        final boolean matched = subject.match(elements);
        assertThat(matched).isTrue();
    }

    interface Bean {
        int int32();
        long int64();
        boolean bool();
        double real();
        String str();
        BigDecimal decimal();
    }

    static class Subject<T> {
        private final Class<T> beanClass;
        private final List<Node<T>> nodes = new ArrayList<>();
        private final Method[] recordedInvocation = { null };
        private final InvocationHandler proxyHandler =
            (proxy, method, args) -> {
                if (method.getName().equals("toString"))
                    return "Proxy";
                recordedInvocation[0] = method;
                return Defaults.defaultValue(method.getReturnType());
            };
        private final T invocationRecorder;

        public Subject(Class<T> type) {
            this.beanClass = type;
            this.invocationRecorder = beanClass.cast(
                Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class<?>[] { beanClass },
                    proxyHandler
                ));
        }

        public Subject<T> add(String value) {
            final String regex = Pattern.quote(value);
            final Pattern pattern = Pattern.compile(regex);
            nodes.add(new Const<T>() {
                @Override
                public String value() {
                    return value;
                }

                @Override
                public Pattern pattern() {
                    return pattern;
                }

                @Override
                public String toString() {
                    return value();
                }
            });
            return this;
        }

        public Subject<T> add(Function<T, ?> getter) {
            getter.apply(invocationRecorder);
            Preconditions.checkState(recordedInvocation[0] != null,
                "No invocation recorded - is it a valid getter indeed?");
            final Method invokedMethod = recordedInvocation[0];
            recordedInvocation[0] = null;
            final String regex = defaultRegex(invokedMethod.getReturnType().getCanonicalName());
            final Pattern pattern = Pattern.compile(regex);
            nodes.add(new PropertyRef<T>() {
                @Override
                public Function<T, ?> getter() {
                    return getter;
                }

                @Override
                public String property() {
                    return invokedMethod.getName();
                }

                @Override
                public Pattern pattern() {
                    return pattern;
                }

                @Override
                public String toString() {
                    return "{" + property()
                           + ":" + regex
                           + '}';
                }
            });
            return this;
        }

        private static String defaultRegex(String jvmTypeName) {
            switch(jvmTypeName) {
                case "java.lang.String":    return "[A-Za-z0-9]+";
                case "int":
                case "java.land.Integer":   return "[+-]?[0-9]+";
                case "long":
                case "java.lang.Long":      return "[+-]?[0-9]+";
                case "double":
                case "java.lang.Double":
                case "java.math.BigDecimal":
                                            return "[-+]?[0-9]+(\\.([0-9]+)?)?";
                case "boolean":
                case "java.lang.Boolean":
                                            return "((?i)true|false)|0|1";
                default:
                    throw new IllegalStateException("Property type not supported: " + jvmTypeName);
            }
        }

        public String render(T bean, String separator) {
            return nodes.stream().reduce(
                new StringBuilder(),
                (sb, node) -> sb.append(separator).append(node.eval(bean)),
                StringBuilder::append
            ).toString();
        }

        public boolean match(Iterator<? extends CharSequence> elements) {
            for (final Node<T> node : nodes) {
                if (!elements.hasNext())
                    return false;
                final CharSequence element = elements.next();
                if (element == null)
                    return false;
                final Matcher matcher = node.pattern().matcher(element);
                if (!matcher.matches())
                    return false;
            }
            return !elements.hasNext();
        }

        interface Node<T> {
            String eval(T bean);
            Pattern pattern();
        }

        interface Const<T> extends Node<T> {
            String value();

            @Override
            default String eval(T bean) { return value(); }
       }

        interface PropertyRef<T> extends Node<T> {
            Function<T, ?> getter();

            String property();

            @Override
            default String eval(T bean) {
                return String.valueOf(getter().apply(bean));
            }
        }
    }
}
