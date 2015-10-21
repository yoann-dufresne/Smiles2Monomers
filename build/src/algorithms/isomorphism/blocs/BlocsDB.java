package algorithms.isomorphism.blocs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.Residue;
import db.DB;

public class BlocsDB extends DB<Bloc> {
	
	private ArrayList<Set<Bloc>> databaseBySize;
	private Map<Bloc, Integer> frequencies;
	private Map<Bloc, List<MappedBloc>> blocMappings;
	private Map<Integer, List<MappedBloc>> residueMappings;

	public BlocsDB() {
		super();
		this.databaseBySize = new ArrayList<>();
		this.frequencies = new HashMap<>();
		this.blocMappings = new HashMap<>();
		this.residueMappings = new HashMap<>();
	}

	public void addObject (String id, Bloc b) {
		super.addObject(id, b);
		
		int diff = b.getSize() - this.databaseBySize.size();
		for (int i=0 ; i<diff ; i++)
			this.databaseBySize.add(new HashSet<Bloc>());
		
		Set<Bloc> blocsForASize = this.databaseBySize.get(b.getSize() - 1);
		blocsForASize.add(b);
		
		if (!this.blocMappings.containsKey(b))
			this.blocMappings.put(b, new ArrayList<MappedBloc>());
	}
	
	public int getFrequency (Bloc b) {
		if (this.frequencies.containsKey(b))
			return this.frequencies.get(b);
		else
			return 0;
	}
	
	public Map<Bloc, Integer> getFrequencies () {
		return new HashMap<>(this.frequencies);
	}
	
	public void setFrequency (Bloc b, int frq) {
		if (this.database.containsValue(b))
			this.frequencies.put(b, frq);
	}
	
	public void setFrequencies (Map<Bloc, Integer> frequencies) {
		for (Entry<Bloc, Integer> e : frequencies.entrySet())
			this.setFrequency(e.getKey(), e.getValue());
	}
	
	public int i = 1;
	public void setMapping (MappedBloc mb) {
		Bloc b = mb.getBloc();
		this.addObject(b.getSerial(), b);
		
		// Add to the blocs mappings
		List<MappedBloc> bmbs = this.blocMappings.get(b);
		if (!bmbs.contains(mb))
			bmbs.add(mb);
		
		// Add to the residues mappings
		Integer idx = new Integer (mb.getChemObject().getId());
		List<MappedBloc> rmbs = this.residueMappings.containsKey(idx) ?
				this.residueMappings.get(idx) : new ArrayList<MappedBloc>();
		this.residueMappings.put(idx, rmbs);
		if (!rmbs.contains(mb))
			rmbs.add(mb);
	}
	
	public void setMappings (List<MappedBloc> mbs) {
		for (MappedBloc mb : mbs)
			this.setMapping(mb);
	}
	
	public void removeObject (Bloc b) {
		Set<Bloc> blocsOfSize = this.databaseBySize.get(b.getSize()-1);
		blocsOfSize.remove(b);
		this.database.remove(b.getSerial());
		this.frequencies.remove(b);
		this.blocMappings.remove(b);
		
		for (List<MappedBloc> mbs : this.residueMappings.values()) {
			List<MappedBloc> toDelete = new ArrayList<>();
			
			for (MappedBloc mb : mbs)
				if (mb.getBloc().equals(b))
					toDelete.add(mb);
			
			for (MappedBloc mb : toDelete)
				mbs.remove(mb);
		}
	}
	
	public List<MappedBloc> getAllMappedBlocsOfSize (int size) {
		List<MappedBloc> mbs = new ArrayList<>();
		
		for (Bloc b : this.databaseBySize.get(size - 1)) {
			mbs.addAll(this.blocMappings.get(b));
		}
		
		return mbs;
	}
	
	public List<MappedBloc> getAllMappedBlocs () {
		List<MappedBloc> mbs = new ArrayList<>();
		
		for (List<MappedBloc> list : this.blocMappings.values())
			mbs.addAll(list);
		
		return mbs;
	}
	
	public List<MappedBloc> getMappedblocsOf (Residue res) {
		if (this.residueMappings.containsKey(new Integer(res.getId())))
			return this.residueMappings.get(new Integer(res.getId()));
		else
			return new ArrayList<>();
	}

	public Set<Bloc> getAllBlocsOfSize(int size) {
		return new HashSet<Bloc>(this.databaseBySize.get(size-1));
	}
	
	public List<Bloc> getAllBlocsOfSize() {
		List<Bloc> blocs = new ArrayList<>();
		
		for (Set<Bloc> blocsOfSize : this.databaseBySize)
			blocs.addAll(blocsOfSize);
		
		return blocs;
	}
	
	public Map<Integer, List<MappedBloc>> getResidueMappings() {
		return residueMappings;
	}

	@Override
	public DB<Bloc> createNew() {
		return new BlocsDB();
	}
	
}
