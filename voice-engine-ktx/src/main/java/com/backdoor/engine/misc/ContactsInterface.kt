package com.backdoor.engine.misc

interface ContactsInterface {
  fun findEmail(input: String?): ContactOutput?
  fun findNumber(input: String?): ContactOutput?
}