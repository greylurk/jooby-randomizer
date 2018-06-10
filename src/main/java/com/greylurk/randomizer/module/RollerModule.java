package com.greylurk.randomizer.module;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.greylurk.randomizer.services.RandomTable;
import com.typesafe.config.Config;

import org.apache.commons.io.IOUtils;
import org.jooby.Env;
import org.jooby.Jooby;
import org.jooby.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RollerModule implements Jooby.Module {
    static final Logger logger = LoggerFactory.getLogger(RollerModule.class);

	@Override
	public void configure(Env env, Config conf, Binder binder) throws Throwable {
        TypeReference<HashMap<String,Object>> outputType = new TypeReference<HashMap<String,Object>>() {};
        InputStream inputStream = RollerModule.class.getResourceAsStream("/tables.json");
        Map<String, Object> table = new ObjectMapper().readValue(inputStream, outputType);

        RandomTable randomTable = new RandomTable(table);

        Router router = env.router();
        router.get("/table/{name}", req -> {
            String tableName = req.param("name").value();
            return randomTable.expandValue(tableName);
        });
    }



}
