.class public hello
.super java/lang/Object
.method public <init>()V
    aload_0
    invokenonvirtual java/lang/Object/<init>()V
    return
.end method
.method public static main([Ljava/lang/String;)V
    .limit stack 20
    .limit locals 3
	getstatic java/lang/System/out Ljava/io/PrintStream;

	ldc "Hello, World!"
	invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
	ldc2_w 3.0
	dstore 1
	getstatic java/lang/System/out Ljava/io/PrintStream;

	dload 1
	ldc2_w 2.0
	dadd
	invokevirtual java/io/PrintStream/println(D)V
	getstatic java/lang/System/out Ljava/io/PrintStream;

	ldc "Hello "
	ldc " "
	invokevirtual java/lang/String/concat(Ljava/lang/String;)Ljava/lang/String;
	ldc "World"
	invokevirtual java/lang/String/concat(Ljava/lang/String;)Ljava/lang/String;
	invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
	dload 1
	ldc2_w 3.0
	dcmpl
	ifne ELSE0
	THEN0:
	getstatic java/lang/System/out Ljava/io/PrintStream;

	ldc "Yay!"
	invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
	goto END0
	ELSE0:
	getstatic java/lang/System/out Ljava/io/PrintStream;

	ldc "Boo."
	invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
	END0:
	dload 1
	ldc2_w 4.0
	dcmpl
	ifle END1
	THEN1:
	getstatic java/lang/System/out Ljava/io/PrintStream;

	ldc "You shouldn't see this."
	invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
	END1:
    return
.end method
