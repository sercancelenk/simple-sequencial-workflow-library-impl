**Simple Workflow Engine in Java**

**Features**
- This library is written for managing sequencial flow steps for your own business.
- It also supports both of future and reactive.
- AbstractContext object will be share for steps. If you would like to put any object to it, you can do and use in step.

**Usage**

```
    @Bean
    public WorkFlow registerWorkflow(){
        WorkflowStep step1 = new CreateUserStep();
        WorkflowStep step2 = new SendMailStep();

        return new WorkFlow("Register Flow", Arrays.asList(step1, step2));
    }

```

```
    @Autowired WorkFlow registerWorkFlow;

    AbstractContext context = new ParameterContext(Optional.of(new HashMap<String, Object>(){{put("sercan", "celenk");}}));
    registerWorkflow.processWorkflow(context).subscribe();
```
