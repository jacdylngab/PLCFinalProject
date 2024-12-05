.class public P4
.super java/lang/Object
.method public <init>()V
    aload_0
    invokenonvirtual java/lang/Object/<init>()V
    return
.end method
.method public static main([Ljava/lang/String;)V
    .limit stack 8
    .limit locals 3
	ldc "Hello"
	astore 1
	ldc2_w 3.0
	dstore 1
	ldc2_w 3.0
	dstore 1
	dload 1
	ldc2_w 1.0
	dadd
	dstore 1
	dload 1
	ldc2_w 1.0
	dadd
	dstore 1
	getstatic java/lang/System/out Ljava/io/PrintStream;

	dload 1
	invokevirtual java/io/PrintStream/println(D)V
    return
.end method
