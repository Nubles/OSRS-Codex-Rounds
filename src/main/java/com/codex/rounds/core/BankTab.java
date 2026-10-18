package com.codex.rounds.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The round's pull list as a bank tab: each kind of supply (teleports, tools, seeds, compost and so on) starts a
 * fresh row of the bank's eight columns, in the order the list shows them, so the withdraw is a sweep down the tab.
 */
public final class BankTab
{
	public static final int COLUMNS = 8;

	private BankTab()
	{
	}

	/** Bank positions to item ids, -1 for a gap. Each line shows the variant the bank holds, else the first. */
	public static int[] layout(List<Need> pulls, Map<Integer, Integer> bank)
	{
		List<Integer> out = new ArrayList<>();
		Need.Kind kind = null;
		for (Need n : pulls)
		{
			if (n.getKind() != kind && kind != null)
			{
				while (out.size() % COLUMNS != 0)
				{
					out.add(-1);
				}
			}
			kind = n.getKind();
			int show = n.getItemId();
			for (int id : n.getItems())
			{
				if (bank.getOrDefault(id, 0) > 0)
				{
					show = id;
					break;
				}
			}
			if (show >= 0 && !out.contains(show))
			{
				out.add(show);
			}
		}
		int[] a = new int[out.size()];
		for (int i = 0; i < a.length; i++)
		{
			a[i] = out.get(i);
		}
		return a;
	}

	/** Every item that answers some line, so any variant in the bank shows in the tab. */
	public static Set<Integer> items(List<Need> pulls)
	{
		Set<Integer> out = new LinkedHashSet<>();
		for (Need n : pulls)
		{
			Arrays.stream(n.getItems()).forEach(out::add);
		}
		return out;
	}
}
