package de.uka.ilkd.key.macros.scripts.meta;

import de.uka.ilkd.key.macros.scripts.ProofScriptCommand;

import java.lang.reflect.Field;

/**
 * @author Alexander Weigl
 * @version 1 (21.04.17)
 */
public class ProofScriptArgument<T> {
    private ProofScriptCommand<T> command;
    private String name;
    private Class type;
    private boolean required;
    private boolean flag;
    private Field field;
    private boolean hasVariableArguments;

    public ProofScriptCommand<T> getCommand() {
        return command;
    }

    public ProofScriptArgument setCommand(ProofScriptCommand<T> command) {
        this.command = command;
        return this;
    }

    public String getName() {
        return name;
    }

    public ProofScriptArgument setName(String name) {
        this.name = name;
        return this;
    }

    public Class getType() {
        return type;
    }

    public ProofScriptArgument setType(Class type) {
        this.type = type;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public ProofScriptArgument setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public boolean isFlag() {
        return flag;
    }

    public ProofScriptArgument setFlag(boolean flag) {
        this.flag = flag;
        return this;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ProofScriptArgument<?> that = (ProofScriptArgument<?>) o;

        if (required != that.required)
            return false;
        if (flag != that.flag)
            return false;
        if (command != null ?
                !command.equals(that.command) :
                that.command != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        return type == that.type;
    }

    @Override public int hashCode() {
        int result = command != null ? command.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (required ? 1 : 0);
        result = 31 * result + (flag ? 1 : 0);
        return result;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public ProofScriptArgument setVariableArguments(boolean hasVariableArguments) {
        this.hasVariableArguments = hasVariableArguments;
        return this;
    }

    public boolean hasVariableArguments() {
        return hasVariableArguments;
    }
}