.class public P4
.super java/lang/Object
.method public <init>()V
    aload_0
    invokenonvirtual java/lang/Object/<init>()V
    return
.end method
.method public static main([Ljava/lang/String;)V
    .limit stack 3
    .limit locals 4
	ldc "Hello"
	astore 1
	getstatic java/lang/System/out Ljava/io/PrintStream;

	aload 1
	invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
	ldc 0.0
	fstore 2
    return
.end method
