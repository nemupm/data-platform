class WordCooccurrence
  include Cequel::Record

  key :word, :text
  column :cnt, :int
  map :co_cnt, :text, :int
  map :updated_at, :text, :timestamp
end
