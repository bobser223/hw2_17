package com.example.di.computer

class Computer {
    private val processor = Processor()
    private val memory = Memory()
    private val storage = Ssd()

    fun boot() {
        processor.start()
        memory.initialize()
        storage.readBootData()
    }
}