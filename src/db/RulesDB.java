package db;

import model.Rule;

public class RulesDB extends DB<Rule> {

	public RulesDB() {
		super();
	}

	@Override
	public DB<Rule> createNew() {
		return new RulesDB();
	}
	
}
