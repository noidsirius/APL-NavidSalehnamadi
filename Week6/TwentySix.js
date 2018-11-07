var fs = require('fs');

class WordFrequencyFramework{
	constructor(){
		this.load_event_handlers = [];
		this.dowork_event_handlers = [];
		this.end_event_handlers = [];
	}
	register_for_load_event(handler){
		this.load_event_handlers.push(handler)
	}
	register_for_dowork_event(handler){
		this.dowork_event_handlers.push(handler)
	}
	register_for_end_event(handler){
		this.end_event_handlers.push(handler)
	}
	run(path_to_file){
		for(var i=0; i< this.load_event_handlers.length; i++){
			this.load_event_handlers[i](path_to_file);
		}
		for(var i=0; i< this.dowork_event_handlers.length; i++){
			this.dowork_event_handlers[i]();
		}
		for(var i=0; i< this.end_event_handlers.length; i++){
			this.end_event_handlers[i](path_to_file);
		}

	}	
}

function StopWordFilter(wfapp){
	this.stop_words = [];
	this.load = function(me){
		return function(ignore){
			var content = fs.readFileSync('../stop_words.txt', 'utf8');
			me.stop_words = content.split(',');
			for(var i=0; i<26; i++)
				me.stop_words.push(String.fromCharCode('a'.charCodeAt(0)+i));
		}};
	wfapp.register_for_load_event(this.load(this));
	this.is_stop_word = function(word){
		return this.stop_words.includes(word);
	}
}

function DataStorage(wfapp, stop_word_filter){
	this.data = []
		this.stop_word_filter = stop_word_filter;
	this.load = function(me){ return function(path_to_file){
		content = fs.readFileSync(path_to_file, 'utf8');
		me.data = content.replace(/[^0-9a-zA-Z]/gi, ' ').toLowerCase().match(/\S+/g);
	};};
	this.produce_words = function(me) {return function(){
		for(var j in me.data){
			var word = me.data[j];
			if(!me.stop_word_filter.is_stop_word(word))
				for(var i=0; i<me.word_event_handlers.length; i++)
					me.word_event_handlers[i](word);
		}
	};};	
	wfapp.register_for_load_event(this.load(this));
	wfapp.register_for_dowork_event(this.produce_words(this));
	this.word_event_handlers = [];
	this.register_for_word_event = function(handler){
		this.word_event_handlers.push(handler);
	}
}

function WordFrequencyCounter(wfapp, data_storage){
	this.word_freqs = {};
	this.increment_count = function(me){ return function(word){
		me.word_freqs[word] = me.word_freqs[word] ? me.word_freqs[word]+1 : 1;
	};};
	this.print_freqs = function(me){ return function(){
		wf_list = Object.keys(me.word_freqs).map(function(key) { return [key, me.word_freqs[key]]})
			wfs = wf_list.sort(function(a,b){ return a[1] < b[1] ? 1 : (a[1] == b[1] ? 0 : -1)});
		for(i in wfs.slice(0, 25)){
			console.log(wfs[i][0] + " - " + wfs[i][1]);
		}
	};};
	data_storage.register_for_word_event(this.increment_count(this));
	wfapp.register_for_end_event(this.print_freqs(this));
}

function WordZCounter(wfapp, wf_counter){
	this.wf_counter = wf_counter;
	this.print_z_number = function(me){ return function(){
		wf_list = Object.keys(me.wf_counter.word_freqs).map(function(key) { return [key, me.wf_counter.word_freqs[key]]})
		var z_distinct = 0;
		var z_total = 0;
		for(i in wf_list){
			if(wf_list[i][0].includes('z')){
				z_distinct += 1;
				z_total += wf_list[i][1];
			}
		}
		console.log("Z distinct",z_distinct);
		console.log("Z total",z_total);

	};};
	wfapp.register_for_end_event(this.print_z_number(this));	
}

framework = new WordFrequencyFramework();
stop_word_filter = new StopWordFilter(framework);
data_storage = new DataStorage(framework, stop_word_filter);
word_freq_counter = new WordFrequencyCounter(framework, data_storage);
word_z_counter = new WordZCounter(framework, word_freq_counter);
//framework.run(process.argv[2]);

all_words = [null, null];
stop_words = [null, null];
non_stop_words = [null, function(){ return all_words[0].map(function(w){ return stop_words[0].includes(w) ? null : w;});}];
unique_words = [null, function(){return Array.from(new Set(non_stop_words[0].filter(function(w){return w != null;})));}];
counts = [null, function(){return unique_words[0].map(function(word){ return non_stop_words[0].filter(function(w){return w==word;}).length;});}];
sorted_data = [null, function(){ return unique_words[0].map(function(w,i){return [w,counts[0][i]];}).sort(function(a,b){return b[1]-a[1];});}];
all_columns = [all_words,stop_words,non_stop_words,unique_words,counts,sorted_data];

function update(){
	for(i in all_columns){
		if(all_columns[i][1] != null)
			all_columns[i][0] = all_columns[i][1]();
	}
}

all_words[0] = fs.readFileSync(process.argv[2], 'utf8').replace(/[^0-9a-zA-Z]/gi, ' ').toLowerCase().match(/\S+/g);
stop_words[0] = fs.readFileSync('../stop_words.txt', 'utf8').split(',');
for(var i=0; i<26; i++)
	stop_words[0].push(String.fromCharCode('a'.charCodeAt(0)+i));
update()
for(i in sorted_data[0].slice(0, 25)){
	console.log(sorted_data[0][i][0] + " - " + sorted_data[0][i][1]);
}

