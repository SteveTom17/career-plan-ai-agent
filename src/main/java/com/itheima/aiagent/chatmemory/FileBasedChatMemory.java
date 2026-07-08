package com.itheima.aiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 基于文件持久化的对话记忆
 */
public class FileBasedChatMemory implements ChatMemory {
    private final String baseDir;

    private static final ThreadLocal<Kryo> KRYO = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    });

    public FileBasedChatMemory(String dir) {
        this.baseDir = Objects.requireNonNull(dir, "dir must not be null");
        File directory = new File(dir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Failed to create chat memory directory: " + dir);
        }
    }

    @Override
    public synchronized void add(String conversationId, List<Message> messages) {
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        conversationMessages.addAll(messages);
        saveConversation(conversationId, conversationMessages);
    }

    @Override
    public synchronized List<Message> get(String conversationId) {
        return new ArrayList<>(getOrCreateConversation(conversationId));
    }

    public synchronized List<Message> get(String conversationId, int lastN) {
        List<Message> allMessages = get(conversationId);
        if (lastN <= 0) {
            return List.of();
        }
        return allMessages.stream()
                .skip(Math.max(0, allMessages.size() - lastN))
                .toList();
    }

    @Override
    public synchronized void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists() && !file.delete()) {
            throw new IllegalStateException("Failed to delete chat memory file: " + file.getAbsolutePath());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Input input = new Input(new FileInputStream(file))) {
            Object data = KRYO.get().readClassAndObject(input);
            if (data instanceof List<?> list) {
                return (List<Message>) list;
            }
            throw new IllegalStateException("Unexpected chat memory content in file: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read chat memory: " + file.getAbsolutePath(), e);
        }
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            KRYO.get().writeClassAndObject(output, messages);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save chat memory: " + file.getAbsolutePath(), e);
        }
    }

    private File getConversationFile(String conversationId) {
        String safeConversationId = Objects.requireNonNull(conversationId, "conversationId must not be null")
                .replaceAll("[\\\\/:*?\"<>|]", "_");
        return new File(baseDir, safeConversationId + ".kryo");
    }
}
