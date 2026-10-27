import java.util.HashMap;
import java.util.Map;
public final class MemoryTemplateRepository implements TemplateRepository {
    private final Map<Long, Template> data = new HashMap<>();
    public Template find(long id) { return data.get(id); }
    public void save(Template template) { data.put(template.getId(), template); }
}
