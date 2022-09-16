package at.willhaben.driving.adapter

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class TestController (
    @Value("\${api.hello.value}") val hellValue: String
){

    @GetMapping("/")
    fun hello(): String {
        return "Hello ${hellValue}"
    }
}
