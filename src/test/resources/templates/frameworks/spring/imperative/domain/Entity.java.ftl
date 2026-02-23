package ${packageName};

public class ${entityName} {
<#list fields as field>
    private ${field.type} ${field.name};
</#list>

<#if hasId>
    private ${idType} id;
</#if>
}
