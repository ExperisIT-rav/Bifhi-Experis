/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.indexes;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.IdolFindApplication;
import com.hp.autonomy.frontend.find.core.indexes.AbstractIndexesServiceIT;
import com.hp.autonomy.types.idol.Database;
import org.springframework.boot.test.SpringApplicationConfiguration;

@SpringApplicationConfiguration(classes = IdolFindApplication.class)
public class IdolIndexesServiceIT extends AbstractIndexesServiceIT<Database, AciErrorException> {
}
