.class public hw4
.super java/lang/Object
.method public <init>()V
    aload_0
    invokenonvirtual java/lang/Object/<init>()V
    return
.end method
.method public static main([Ljava/lang/String;)V
    .limit stack 8
    .limit locals 3
	ldc ""
	astore 1
	getstatic java/lang/System/out Ljava/io/PrintStream;

	ldc "Enter your name:"
	invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
	new java/util/Scanner

	dup

	getstatic java/lang/System/in Ljava/io/InputStream;

	invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V

	invokevirtual java/util/Scanner/nextLine()Ljava/lang/String;

	astore 1
	getstatic java/lang/System/out Ljava/io/PrintStream;

	ldc "Hello "
	aload 1
	invokevirtual java/lang/String/concat(Ljava/lang/String;)Ljava/lang/String;
	invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
    return
.end method
