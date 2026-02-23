package ${packageName};

import org.springframework.stereotype.Component;

@Component
public class ${adapterName} {
<#list methods as method>
    public ${method.returnType} ${method.name}(<#list method.parameters as param>${param.type} ${param.name}<#sep>, </#sep></#list>) {
        return null;
    }
</#list>
}
