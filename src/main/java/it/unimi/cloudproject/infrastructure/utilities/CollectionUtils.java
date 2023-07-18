package it.unimi.cloudproject.infrastructure.utilities;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtils {
    // from https://www.techiedelight.com/identify-duplicates-list-java/
    public static <T> Set<T> findDuplicates(Collection<T> list)
    {
        Map<T, Long> frequencyMap = list.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return frequencyMap.keySet().stream()
                .filter(key -> frequencyMap.get(key) > 1)
                .collect(Collectors.toSet());
    }
}
