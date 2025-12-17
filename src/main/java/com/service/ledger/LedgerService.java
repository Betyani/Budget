package com.service.ledger;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.model.ledger.LedgerItem;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LedgerService {

    private static final Path FILE = Paths.get("data", "ledger.json");
    private static final Type LIST_TYPE = new TypeToken<List<LedgerItem>>() {}.getType();

    private final Gson gson;

    public LedgerService() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    public void add(LedgerItem item) {
        List<LedgerItem> all = loadAll();
        all.add(item);
        saveAll(all);
    }

    public List<LedgerItem> findByDate(LocalDate date) {
        List<LedgerItem> all = loadAll();
        List<LedgerItem> result = new ArrayList<>();
        for (LedgerItem item : all) {
            if (date.equals(item.getDate())) {
                result.add(item);
            }
        }
        return result;
    }

    /* ===== 내부 저장/로드 ===== */

    private List<LedgerItem> loadAll() {
        try {
            if (!Files.exists(FILE)) return new ArrayList<>();
            String json = Files.readString(FILE);
            List<LedgerItem> data = gson.fromJson(json, LIST_TYPE);
            return data == null ? new ArrayList<>() : data;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveAll(List<LedgerItem> data) {
        try {
            Files.createDirectories(FILE.getParent());
            String json = gson.toJson(data, LIST_TYPE);
            Files.writeString(FILE, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ===== LocalDate 어댑터 ===== */

    private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString()); // "2025-12-01"
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return LocalDate.parse(json.getAsString());
        }
    }
}
