# 🔧 PLC Final Project – Dam Language Compiler Enhancements

This project extends the functionality of the **Dam language compiler**, building on the foundation laid in HW4. New features include full support for variable assignment, `let` statements, typecasting, `while` loops, and robust truthy evaluation in control flow constructs — all compiled to Jasmin bytecode.

## 📌 Project Goals

✅ Extend the Dam language with:

- ✔️ Functional `let` statements for variable declarations
- ✔️ Support for reassignment of variables to any type (`int`, `string`, `double`, `bool`, etc.)
- ✔️ `while` loop support for control flow
- ✔️ Typecasting with `str()`, `double()`, and `bool()` as syntactic sugar
- ✔️ Truthy/falsy evaluation in `if` statements
- ✔️ Graceful error handling (no raw Jasmin errors shown to the user)

---

## ✨ Language Features

### ✅ Variable Declaration and Assignment

```dam
let s;        // Declares a string variable with default value ""
let t = "";   // Declares and initializes a variable

s = "Hello";
s = 3.0;      // Variables can be reassigned to values of any type
```

### 🔁 While Loops
```
let x = 0;
while (x < 5) {
    print x;
    x = x + 1;
}
```

### 🔄 Typecasting (Syntactic Sugar)
```
let s = str(3.0);          // "3.0"
let x = double(s);         // 3.0
let b1 = bool(x);          // true
let b2 = bool(1.0 + 2.0);  // true
```

### 🔂 Truthy Conditions in If Statements
```
if ("non-empty") {
    print "I should see this.";
}

if (0.0) {
    print "I should NOT see this.";
}
```
