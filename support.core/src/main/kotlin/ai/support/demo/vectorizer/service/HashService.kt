package ai.support.demo.vectorizer.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class HashService {
    @Value("\${chunk.hash.algo}")
    lateinit var algorithm: String;

    fun hashString(input: String) : String {
        return hashString(input, algorithm)
    }

    private fun hashString(input: String, algorithm: String): String {
        return MessageDigest
            .getInstance(algorithm)
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

}