export interface Word {
  name: string;
  relatedWords: RelatedWord[];
  cnt: number;
}

export interface RelatedWord {
  name: string;
  score: number;
  cnt: number;
}
