package ${packageName};

public interface ${useCaseName} {
<#list methods as method>
    ${method.returnType} ${method.name}(<#list method.parameters as param>${param.type} ${param.name}<#sep>, </#sep></#list>);
</#list>
}
