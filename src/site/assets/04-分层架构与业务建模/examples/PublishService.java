public final class PublishService {
    private final TemplateRepository repository;
    public PublishService(TemplateRepository repository) {
        this.repository = repository;
    }
    public void publish(long id) {
        Template template = repository.find(id);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在");
        }
        template.publish();
        repository.save(template);
    }
}
