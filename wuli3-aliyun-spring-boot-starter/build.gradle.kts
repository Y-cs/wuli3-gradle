plugins {
    id("com.kjs.wuli3.spring-conventions")
}

dependencies {
    implementation(project(":wuli3-core"))
    api("com.aliyun:alibabacloud-oss-v2")
}
