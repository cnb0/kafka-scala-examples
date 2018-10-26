package com.kafka.demo

import org.scalatest.{Matchers, WordSpecLike}

final class AvroCodeGenerationSpec extends WordSpecLike with Matchers {

  private[this] def getUsers(): List[User] = {
    // Leave favorite color null
    val user1 = new User()
    user1.setName("Alyssa")
    user1.setFavoriteNumber(256)

    // Alternate constructor
    val user2 = new User("Ben", 7, "red")

    // Construct via builder
    val user3 = User.newBuilder()
      .setName("Charlie")
      .setFavoriteColor("blue")
      .setFavoriteNumber(null)
      .build()

    List(user1, user2, user3)
  }

  "AvroCodeGeneration" should {

    "serialize and deserialize" in {
      val filePath = "data/users-code-generation.avro"
      val users = getUsers()

      AvroCodeGeneration.serializeUsers(users, filePath)
      AvroCodeGeneration.deserializeUsers(filePath) shouldBe users
    }

  }

}