/*
 *  This file is part of CubicChunksConverter, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2017 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package cubicchunks.converter.lib;

import com.flowpowered.nbt.ByteTag;
import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.StringTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.flowpowered.nbt.stream.NBTOutputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AnvilToCCLevelInfoConverter implements Runnable {

	private final Path srcDir;
	private final Path dstDir;

	public AnvilToCCLevelInfoConverter(Path srcDir, Path dstDir) {
		this.srcDir = srcDir;
		this.dstDir = dstDir;
	}

	@Override public void run() {
		CompoundTag root;
		try (NBTInputStream nbtIn = new NBTInputStream(new FileInputStream(srcDir.resolve("level.dat").toFile()));
		     NBTOutputStream nbtOut = new NBTOutputStream(new FileOutputStream(dstDir.resolve("level.dat").toFile()));) {
			root = (CompoundTag) nbtIn.readTag();

			CompoundMap newRoot = new CompoundMap();
			for (Tag<?> tag : root.getValue()) {
				if (tag.getName().equals("Data")) {
					CompoundMap data = ((CompoundTag) root.getValue().get("Data")).getValue();
					CompoundMap newData = new CompoundMap();
					for (Tag<?> dataTag : data) {
						if (dataTag.getName().equals("generatorName")) {
							String value = (String) dataTag.getValue();
							String newValue;
							if (value.equalsIgnoreCase("default")) {
								newValue = "VanillaCubic";
							} else {
								newValue = value;
							}
							newData.put(new StringTag(dataTag.getName(), newValue));
						} else {
							newData.put(dataTag);
						}
					}
					// put isCubicWorld at the end to overwrite previously existing data, if any
					newData.put("isCubicWorld", new ByteTag("isCubicWorld", (byte) 1));
					newRoot.put(new CompoundTag(tag.getName(), newData));
				} else {
					newRoot.put(tag);
				}
			}
			Files.createDirectories(dstDir);

			nbtOut.writeTag(new CompoundTag(root.getName(), newRoot));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
