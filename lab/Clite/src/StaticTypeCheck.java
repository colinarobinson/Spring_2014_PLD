// StaticTypeCheck.java

import java.util.*;

// Static type checking for Clite is defined by the functions 
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.


public class StaticTypeCheck {

    public static TypeMap typing (Declarations d) {
        TypeMap map = new TypeMap();
        for (Declaration di : d) 
            map.put (di.v, di.t);
        return map;
    }

    public static void check(boolean test, String msg) {
        if (test)  return;
        System.err.println(msg);
        System.exit(1);
    }

    public static void V (Declarations d) {
        for (int i=0; i<d.size() - 1; i++)
            for (int j=i+1; j<d.size(); j++) {
                Declaration di = d.get(i);
                Declaration dj = d.get(j);
                check( ! (di.v.equals(dj.v)),
                       "duplicate declaration: " + dj.v);
            }
    } 

    public static void V (Program p) {
        V (p.decpart);
        V (p.body, typing (p.decpart));
    } 

    public static Type typeOf (Expression e, TypeMap tm) {
        if (e instanceof Value) return ((Value)e).type;
        if (e instanceof Variable) {
            Variable v = (Variable)e;
            check (tm.containsKey(v), "undefined variable: " + v);
            return (Type) tm.get(v);
        }
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            if (b.op.ArithmeticOp( ))
                if (typeOf(b.term1,tm)== Type.FLOAT)
                    return (Type.FLOAT);
                else return (Type.INT);
            if (b.op.RelationalOp( ) || b.op.BooleanOp( ))
                return (Type.BOOL);
        }
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            if (u.op.NotOp( ))         return (Type.BOOL);
            else if (u.op.NegateOp( )) return typeOf(u.term,tm);
            else if (u.op.intOp( ))    return (Type.INT);
            else if (u.op.floatOp( ))  return (Type.FLOAT);
            else if (u.op.charOp( ))   return (Type.CHAR);
        }
        throw new IllegalArgumentException("should never reach here");
    } 

    public static void V (Expression e, TypeMap tm) {
        if (e instanceof Value) 
            return;
        if (e instanceof Variable) { 
            Variable v = (Variable)e;
            check( tm.containsKey(v)
                   , "undeclared variable: " + v);
            return;
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Type typ1 = typeOf(b.term1, tm);
            Type typ2 = typeOf(b.term2, tm);
            V (b.term1, tm);
            V (b.term2, tm);
            if (b.op.ArithmeticOp( ))
                check( (typ1 == Type.CHAR && typ2 == Type.INT) || (typ1 == Type.INT && typ2 == Type.CHAR) ||
                       (typ1 == Type.FLOAT && typ2 == Type.INT) || (typ1 == Type.INT && typ2 == Type.FLOAT) ||
                       (typ1 == typ2 && typ1 != Type.BOOL)
                       , "type error for " + b.op);
            else if (b.op.RelationalOp( ))
                check( typ1 == typ2 , "type error for " + b.op);
            else if (b.op.BooleanOp( ))
                check( typ1 == Type.BOOL && typ2 == Type.BOOL,
                       b.op + ": non-bool operand");
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        }
        if (e instanceof Unary) {
            Unary u = (Unary) e;
	    V(u.term, tm);
	    Type type = typeOf(u.term, tm);
	    check( (u.op.equals( Operator.NOT ) && type == Type.BOOL) ||
		   (u.op.equals( Operator.NEG ) && (type == Type.INT || type == Type.FLOAT)) ||
		   (u.op.equals( Operator.FLOAT ) && (type == Type.INT || type == Type.CHAR)) ||
		   (u.op.equals( Operator.INT ) && (type == Type.FLOAT || type == Type.CHAR)) ||
		   (u.op.equals( Operator.CHAR ) && type == Type.INT),
		   "Invalid unary operator " + u.op + " of type " + type);
	    return;
        }
        throw new IllegalArgumentException("should never reach here: expression");
    }

    public static void V (Statement s, TypeMap tm) {
        if ( s == null )
            throw new IllegalArgumentException( "AST error: null statement");
        if (s instanceof Skip) return;
        if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            check( tm.containsKey(a.target)
                   , " undefined target in assignment: " + a.target);
            V(a.source, tm);
            Type ttype = (Type)tm.get(a.target);
            Type srctype = typeOf(a.source, tm);
            if (ttype != srctype) {
                if (ttype == Type.FLOAT)
                    check( srctype == Type.INT
                           , "mixed mode assignment to " + a.target);
                else if (ttype == Type.INT)
                    check( srctype == Type.CHAR
                           , "mixed mode assignment to " + a.target);
                else
                    check( false
                           , "mixed mode assignment to " + a.target);
            }
            return;
        }
        if (s instanceof Conditional) {
	    Conditional c = (Conditional) s;
	    V(c.test, tm);
	    check(typeOf(c.test, tm) == Type.BOOL, "test expression not of type bool: " + c.test);
	    V(c.thenbranch, tm);
	    V(c.elsebranch, tm);
	    return;
        }
        if (s instanceof Loop) {
	    Loop l = (Loop) s;
	    V(l.test, tm);
	    check(typeOf(l.test, tm) == Type.BOOL, "test expression not of type bool: " + l.test);
	    V(l.body, tm);
	    return;
        }
        if (s instanceof Block) {
	    Block b = (Block) s;
	    for (Statement bs : b.members)
	      V(bs, tm);
	    return;
        }
        throw new IllegalArgumentException("Should never reach here: statement");
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();
        System.out.println("\nBegin type checking...");
        System.out.println("Type map:");
        TypeMap map = typing(prog.decpart);
        map.display();   // student exercise
        V(prog);
    } //main

} // class StaticTypeCheck

