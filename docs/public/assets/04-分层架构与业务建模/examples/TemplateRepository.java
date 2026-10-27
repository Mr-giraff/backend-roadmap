public interface TemplateRepository {
    Template find(long id);
    void save(Template template);
}
