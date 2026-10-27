public class ArchitectureDemo {
    public static void main(String[] args) {
        TemplateRepository repository = new MemoryTemplateRepository();
        repository.save(new Template(3L, "门店销量"));
        PublishService service = new PublishService(repository);
        service.publish(3L);
        System.out.println(repository.find(3L).getStatus());
    }
}
