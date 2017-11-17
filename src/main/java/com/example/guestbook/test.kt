package com.example.guestbook

enum class State
class Transaction(val state: State, val sum: Long)
class Account(var transactions: List<Transaction>, val balance: Double)