#include<iostream>
#include<fstream>
#include<vector>
#include<map>
#include<set>
#include<sstream>
#include<algorithm>
using namespace std;


class DataStorageManager {
	private:
		string content;
		void* init(string file_path){
			ifstream text_file;
			text_file.open(file_path.c_str());
			if(text_file.fail()){
				cerr <<"This file is not accessible" << endl;
				exit(1);
			}
			string line;
			content = "";
			while(getline(text_file, line))
				content += line + '\n';
			text_file.close();
			refine_text(content);
			return NULL;
		}
		void refine_text(string &text){
			for(int i=0; i< text.length(); i++)
				if(isalpha(text[i]) || isspace(text[i]) )
					text[i] = tolower(text[i]);
				else
					text[i] = ' ';
		}
		string* get_content(){
			return new string(content);
		}
	public:
		DataStorageManager(){
		} 
		void* dispatch(string message){
			string cmd = message.substr(0,4);
			if (cmd == "init")
				return init(message.substr(4));
			else if(cmd == "cont")
				return get_content();
			return NULL;
		}
};

class StopWordManager{
	private:
		set<string> stop_words;	
		bool is_blank(string word){
			for(int i=0; i<word.length(); i++)
				if(!isspace(word[i]))
					return false;
			return true;
		}
		void* init(){
			ifstream stop_words_file;
			stop_words_file.open("../stop_words.txt");
			string word;
			while(getline(stop_words_file, word, ',')){
				if(is_blank(word))
					continue;
				stop_words.insert(word);
			}
			stop_words_file.close();
			return NULL;
		}
		bool* is_stop_word(string word){
			return new bool(is_blank(word) or word.length()<2 or stop_words.find(word) != stop_words.end());
		}
	public:
		StopWordManager(){};
		void* dispatch(string message){
			string cmd = message.substr(0,4);
			if (cmd == "init")
				return init();
			else if(cmd == "isSW")
				return is_stop_word(message.substr(4));
			return NULL;
		}
};

class WordFrequencyManager{
	private:
		map<string, int> frequency_words;
		bool sortbysecond(const pair<string,int> &a, const pair<string,int> &b) 
		{ 
			return (a.second > b.second); 
		}
		vector< pair<string, int> >* sort(){
			vector<pair<string, int> > term_frequencies;
			for(map<string,int>::iterator it = frequency_words.begin(); it != frequency_words.end(); it++)
				term_frequencies.push_back(make_pair(it->first, it->second));
			::sort(term_frequencies.begin(), term_frequencies.end(), [](const pair<string,int> &a, const pair<string,int> &b)->bool{return a.second > b.second;});
			return new vector<pair<string, int> >(term_frequencies);
		}
		void* increment_count(string word){
			if(frequency_words.find(word) == frequency_words.end())
				frequency_words[word] = 1;
			else
				frequency_words[word] = frequency_words[word] + 1;
			return NULL;
		}
	public:
		WordFrequencyManager(){}
		void* dispatch(string message){
			string cmd = message.substr(0,4);
			if (cmd == "incW")
				return increment_count(message.substr(4));
			else if(cmd == "sort")
				return sort();	
			return NULL;
		}

};

class WordFrequencyController{
	private:
		DataStorageManager ds_manager;
		StopWordManager sw_manager;
		WordFrequencyManager wf_manager;

		void init(string file_path){
			ds_manager.dispatch("init"+file_path);
			sw_manager.dispatch("init");
		}
		void run(){
			string whole_text = *(string*)ds_manager.dispatch("cont");
			stringstream ss(whole_text);
			string word;
			while(ss >> word){
				if(*(bool*)sw_manager.dispatch("isSW"+word))
					continue;
				wf_manager.dispatch("incW"+word);
			}
			vector< pair<string, int> > tf = *(vector< pair<string,int> > *)wf_manager.dispatch("sort");
			for(int i=0; i<25; i++)
				cout << tf[i].first << "  -   " << tf[i].second << endl;	
		}
	public:
		void dispatch(string message){
			string cmd = message.substr(0,4);
			if (cmd == "init")
				init(message.substr(4));
			else if(cmd == "run!")
				run();	
		}

};

int main(int argc, char *argv[]){
	if(argc != 2)
	{
		cerr << "Not enough arguments" << endl;
		return 1;
	}
	WordFrequencyController wf_controller;
	wf_controller.dispatch("init"+string(argv[1]));
	wf_controller.dispatch("run!");
	return 0;
}
