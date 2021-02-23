/*
 * Tai-e - A Program Analysis Framework for Java
 *
 * Copyright (C) 2020 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2020 Yue Li <yueli@nju.edu.cn>
 * All rights reserved.
 *
 * This software is designed for the "Static Program Analysis" course at
 * Nanjing University, and it supports a subset of Java features.
 * Tai-e is only for educational and academic purposes, and any form of
 * commercial use is disallowed.
 */

package pascal.taie.pta.ir;

/**
 * Represents an array store: base[*] = from.
 */
public class ArrayStore implements Statement {

    private final Variable base;

    private final Variable from;

    public ArrayStore(Variable base, Variable from) {
        this.base = base;
        this.from = from;
        base.addArrayStore(this);
    }

    public Variable getBase() {
        return base;
    }

    public Variable getFrom() {
        return from;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return base + "[*] = " + from;
    }
}
