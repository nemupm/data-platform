class WordCooccurrence
  include Cequel::Record
  include ActiveModel::Model
  include ActiveModel::Attributes

  key :word, :text
  column :cnt, :int
  map :co_cnt, :text, :int
  map :sum_cnt, :text, :int
  map :updated_at, :text, :timestamp

  attribute :word, :string
  attribute :cnt, :integer
  attribute :co_cnt
  attribute :sum_cnt
end
