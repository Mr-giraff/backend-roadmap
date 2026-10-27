import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DirectoryExample {
    interface Named {
        String getName();
    }

    enum Status {
        ACTIVE, ARCHIVED
    }

    static final class Directory implements Named {
        private final long id;
        private final String name;
        private final Status status;

        Directory(long id, String name, Status status) {
            if (id <= 0) {
                throw new IllegalArgumentException("编号必须大于零");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("名称不能为空");
            }
            if (status == null) {
                throw new IllegalArgumentException("状态不能为空");
            }
            this.id = id;
            this.name = name;
            this.status = status;
        }

        long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        boolean isActive() {
            return status == Status.ACTIVE;
        }
    }

    static final class Result<T> {
        private final T data;

        Result(T data) {
            this.data = data;
        }

        T getData() {
            return data;
        }
    }

    static List<String> activeNames(List<Directory> directories) {
        return directories.stream()
                .filter(Directory::isActive)
                .map(Directory::getName)
                .collect(Collectors.toList());
    }

    static Map<Long, Directory> indexById(List<Directory> directories) {
        Map<Long, Directory> index = new LinkedHashMap<>();
        for (Directory directory : directories) {
            if (index.putIfAbsent(directory.getId(), directory) != null) {
                throw new IllegalArgumentException("重复编号：" + directory.getId());
            }
        }
        return index;
    }

    public static void main(String[] args) {
        List<Directory> directories = Arrays.asList(
                new Directory(1L, "ODS", Status.ACTIVE),
                new Directory(2L, "ADS", Status.ACTIVE),
                new Directory(3L, "TMP", Status.ARCHIVED));
        Result<List<String>> result = new Result<>(activeNames(directories));
        System.out.println("可用目录：" + result.getData());
        System.out.println("编号 2：" + indexById(directories).get(2L).getName());
        try {
            indexById(Arrays.asList(directories.get(0), directories.get(0)));
        } catch (IllegalArgumentException e) {
            System.out.println("拒绝重复：" + e.getMessage());
        }
    }
}
