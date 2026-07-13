plugins {
    id("com.kjs.wuli3.java-conventions")
}

description =
    "传播模块，用于定义一套传播策略。目的是为了保障在各种远程协议下传播context，让业务代码在使用的时候不用关心当前的场景和协议。保障用户的访问状态不会因为转发中断。"

dependencies {
    implementation(project(":wuli3-core"))
}
