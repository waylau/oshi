/*
 * Copyright (c) Daniel Widdis, 2015
 * widdis[at]gmail[dot]com
 * All Rights Reserved
 * Eclipse Public License (EPLv1)
 * http://oshi.codeplex.com/license
 */
package oshi.software.os.mac.local;

import oshi.hardware.Memory;
import oshi.software.os.mac.local.SystemB.VMStatistics;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

/**
 * Memory obtained by host_statistics (vm_stat) and sysctl
 * 
 * @author widdis[at]gmail[dot]com
 */
public class GlobalMemory implements Memory {

	long totalMemory = 0;

	public long getAvailable() {
		long availableMemory = 0;
		long pageSize = 4096;

		int machPort = SystemB.INSTANCE.mach_host_self();

		LongByReference pPageSize = new LongByReference();
		if (0 != SystemB.INSTANCE.host_page_size(machPort, pPageSize))
			throw new LastErrorException("Error code: " + Native.getLastError());
		pageSize = pPageSize.getValue();

		VMStatistics vmStats = new VMStatistics();
		if (0 != SystemB.INSTANCE.host_statistics(machPort,
				SystemB.HOST_VM_INFO, vmStats,
				new IntByReference(vmStats.size() / SystemB.INT_SIZE)))
			throw new LastErrorException("Error code: " + Native.getLastError());
		availableMemory = (vmStats.free_count + vmStats.inactive_count)
				* pageSize;

		return availableMemory;
	}

	public long getTotal() {
		if (totalMemory == 0) {
			int[] mib = { SystemB.CTL_HW, SystemB.HW_MEMSIZE };
			Pointer pMemSize = new com.sun.jna.Memory(SystemB.UINT64_SIZE);
			if (0 != SystemB.INSTANCE.sysctl(mib, mib.length, pMemSize,
					new IntByReference(SystemB.UINT64_SIZE), null, 0))
				throw new LastErrorException("Error code: "
						+ Native.getLastError());
			totalMemory = pMemSize.getLong(0);
		}
		return totalMemory;
	}
}
