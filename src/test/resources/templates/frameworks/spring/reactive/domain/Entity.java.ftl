package ${packageName};

public class ${entityName} {
<#list fields as field>
    private ${field.type} ${field.name};
</#list>

<#if hasId>
    private ${idType} id;
</#if>

    // Getters and setters
<#list fields as field>
    public ${field.type} get${field.name?cap_first}() {
        return ${field.name};
    }

    public void set${field.name?cap_first}(${field.type} ${field.name}) {
        this.${field.name} = ${field.name};
    }
</#list>

<#if hasId>
    public ${idType} getId() {
        return id;
    }

    public void setId(${idType} id) {
        this.id = id;
    }
</#if>
}
