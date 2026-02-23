package ${packageName};

import org.springframework.stereotype.Component;

@Component
public class ${adapterName} {

    private final ${entityName}Repository repository;

    public ${adapterName}(${entityName}Repository repository) {
        this.repository = repository;
    }

<#list methods as method>
    public ${method.returnType} ${method.name}(<#list method.parameters as param>${param.type} ${param.name}<#sep>, </#sep></#list>) {
        // Implementation
        return null;
    }

</#list>
}
