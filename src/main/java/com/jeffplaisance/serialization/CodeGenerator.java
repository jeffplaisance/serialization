package com.jeffplaisance.serialization;

import com.jeffplaisance.serialization.ast.Include;
import com.jeffplaisance.serialization.ast.Struct;
import com.jeffplaisance.serialization.ast.Union;

import java.io.Closeable;

/**
 * @author jplaisance
 */
public interface CodeGenerator extends Closeable {

    public void pushModule(String moduleName);

    public void popModule();

    public void addStruct(Struct struct);

    public void addUnion(Union union);

    public void addInclude(Include include);
}
