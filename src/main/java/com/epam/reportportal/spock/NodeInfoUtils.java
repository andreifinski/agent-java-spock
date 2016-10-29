/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/agent-java-spock
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.reportportal.spock;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static org.spockframework.runtime.model.BlockKind.WHERE;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import org.spockframework.runtime.model.*;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import spock.lang.Narrative;
import spock.lang.Title;

/**
 * Created by Dzmitry_Mikhievich
 */
class NodeInfoUtils {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String IDENTIFIER_SEPARATOR = ".";
	private static final String BLOCK_SPLITTER = ": ";

	private static final Map<BlockKind, String> BLOCK_NAMES = Maps.newEnumMap(BlockKind.class);

	private static final String CONJUNCTION_KEYWORD = "And";

	private static final Predicate<BlockInfo> SKIP_BLOCK_CONDITION = new Predicate<BlockInfo>() {
		@Override
		public boolean apply(@Nullable BlockInfo info) {
			if (info != null) {
				boolean isWhereBlock = WHERE.equals(info.getKind());
				if (isWhereBlock) {
					return Iterables.all(info.getTexts(), new Predicate<String>() {
						@Override
						public boolean apply(@Nullable String input) {
							return isNullOrEmpty(input);
						}
					});
				}
			}
			return false;
		}
	};

	private NodeInfoUtils() {
	}

	static String buildFeatureDescription(FeatureInfo featureInfo) {
		StringBuilder description = new StringBuilder();
		Iterator<BlockInfo> blocksIterator = featureInfo.getBlocks().iterator();
		while (blocksIterator.hasNext()) {
			BlockInfo block = blocksIterator.next();
			if (!SKIP_BLOCK_CONDITION.apply(block)) {
                appendBlockInfo(description, block);
				boolean notLast = blocksIterator.hasNext();
				if (notLast) {
					description.append(LINE_SEPARATOR);
				}
			}
		}
		return description.toString();
	}

	@Nullable
	static String retrieveSpecNarrative(SpecInfo specInfo) {
		Narrative narrative = specInfo.getAnnotation(Narrative.class);
		return narrative != null ? narrative.value() : null;
	}

	static String retrieveSpecName(SpecInfo specInfo) {
		Title title = specInfo.getAnnotation(Title.class);
		return title != null ? title.value() : specInfo.getName();
	}

	static String getMethodIdentifier(MethodInfo method) {
		StringBuilder buffer = new StringBuilder(getSpecIdentifier(method.getParent()));
		FeatureInfo featureInfo = method.getFeature();
		if(featureInfo != null) {
			buffer.append(IDENTIFIER_SEPARATOR).append(featureInfo.getName());
		}
		return buffer.append(IDENTIFIER_SEPARATOR).append(method.getName()).toString();
	}

	//TODO optimize
	static String getSpecIdentifier(SpecInfo spec) {
		if(spec != null) {
			return nullToEmpty(spec.getPackage()) + IDENTIFIER_SEPARATOR + spec.getFilename();
		}
		return "";
	}

	private static void appendBlockInfo(StringBuilder featureDescription, BlockInfo block) {
		featureDescription.append(formatBlockKind(block.getKind())).append(BLOCK_SPLITTER);
		Iterator<String> textsIterator = block.getTexts().iterator();
        //append heading block
		if(textsIterator.hasNext()) {
			featureDescription.append(textsIterator.next());
		}
        //append conjunction blocks
		while(textsIterator.hasNext()) {
			featureDescription.append(LINE_SEPARATOR)
					.append(CONJUNCTION_KEYWORD)
					.append(BLOCK_SPLITTER)
					.append(textsIterator.next());
		}
	}

	static String formatBlockKind(BlockKind blockKind) {
		if(BLOCK_NAMES.containsKey(blockKind)) {
			return BLOCK_NAMES.get(blockKind);
		} else {
			char[] initialChars = blockKind.name().toCharArray();
			char[] buffer = new char[initialChars.length];
			buffer[0] = initialChars[0];
			// iterate over characters excluding the first one
			for (int i = 1; i < initialChars.length; i++) {
				char ch = initialChars[i];
				if (Character.isUpperCase(ch)) {
					ch = Character.toLowerCase(ch);
				}
				buffer[i] = ch;
			}
			String blockName = new String(buffer);
			BLOCK_NAMES.put(blockKind, blockName);
			return blockName;
		}
	}
}
