package satd.step2;

class Class1 {
    void method1(int code) {
        if (cod > 20)
            System.out.println(String.format("code is MODIFIED %d", code));
        for(int i = 0; i<20;i++)
            method2();
    }

    double method2() {
        int offset = 10;
        return java.lang.Math.random() + offset;
    }

}