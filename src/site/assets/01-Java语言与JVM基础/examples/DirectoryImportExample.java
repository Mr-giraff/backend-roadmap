import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectoryImportExample {
    static long validate(String line) {
        String[] columns = line.split(",", -1);
        if (columns.length != 2) {
            throw new IllegalArgumentException("需要两列：编号,名称");
        }
        long id;
        try {
            id = Long.parseLong(columns[0].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("编号不是有效的 long 整数", e);
        }
        if (id <= 0) {
            throw new IllegalArgumentException("编号必须大于零");
        }
        if (columns[1].trim().isEmpty()) {
            throw new IllegalArgumentException("名称不能为空");
        }
        return id;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("请传入输入文件路径");
        }
        Path path = Paths.get(args[0]);
        int valid = 0;
        int invalid = 0;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    validate(line);
                    valid++;
                } catch (IllegalArgumentException e) {
                    invalid++;
                    System.out.println("第 " + lineNumber + " 行：" + e.getMessage());
                }
            }
        }
        System.out.println("有效 " + valid + " 行，无效 " + invalid + " 行");
    }
}
