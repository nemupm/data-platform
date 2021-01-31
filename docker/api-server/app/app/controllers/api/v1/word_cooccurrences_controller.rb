module Api
    module V1
        class WordCooccurrencesController < ApplicationController
            before_action :set_data, only: [:show]

            def show
                render json: { status: 'SUCCESS', data: @data }
            end

            private

            def set_data
                @data = WordCooccurrence.find_by_word(params[:word])
            end
        end
    end
end